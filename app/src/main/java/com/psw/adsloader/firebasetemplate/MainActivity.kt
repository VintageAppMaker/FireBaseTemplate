package com.psw.adsloader.firebasetemplate

import android.content.Context
import android.graphics.Bitmap
import android.net.Uri
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.widget.ImageView
import com.bumptech.glide.Glide
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import kotlinx.android.synthetic.main.activity_main.*
import java.io.ByteArrayOutputStream
import java.util.*
import android.graphics.ColorMatrixColorFilter
import android.graphics.ColorMatrix



class MainActivity : AppCompatActivity() {

    var  mAuth : FirebaseAuth? = null

    data class Item(var name : String, var img : String, var mp3 : String )
    private val posts: MutableList<Item> = mutableListOf()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        mAuth = FirebaseAuth.getInstance()

        txtLogin.setOnClickListener {
            LoginUserInfo("snake2@caver.com", "123456mode6")
        }

        btnCreate.setOnClickListener {
            CreateUserInfo("snake2@caver.com", "123456mode6")
        }

        // Realtime Database에 값을 추가하기
        btnSetData.setOnClickListener {
            var fd  = FirebaseDatabase.getInstance()
            var ref = fd.getReference("playlist").push()

            val item = Item(
                    mAuth!!.getCurrentUser().toString(),
                    mAuth!!.getCurrentUser().toString(),
                    mAuth!!.getCurrentUser().toString())

            ref.setValue(item)
        }

        // Realtime Database에 리스트를 가져오기(당황스러움! 많이!!)
        // 현실적으로 사용하기 무척 불쾌함.
        btnGetData.setOnClickListener {
            var fd  = FirebaseDatabase.getInstance()
            var ref = fd.getReference("playlist")
            ref.addListenerForSingleValueEvent( object : ValueEventListener {
                override fun onCancelled(p0: DatabaseError) {

                }

                override fun onDataChange(p0: DataSnapshot) {
                    posts.clear()

                    // 한번에 긁어온다.
                    var str = ""
                    p0.children.forEach {
                        str = str + "\n" + it.toString()

                    }
                    WriteLn(str)

                }
            })

        }

        // firebase의 라이브러리간 버전호환은 아주 안좋다.
        // implementation 'com.google.firebase:firebase-firestore:11.8.0' 기준임.
        btnFireStoreSet.setOnClickListener {
            val myDB = FirebaseFirestore.getInstance()
            val play = myDB.collection("play")

            // 1. 도큐먼트로 추가(존재하면 수정)
            play.document("song").set( mapOf(
                    "title"     to  "문서가 하나만 존재함",
                    "music_url" to  "music 입니다",
                    "pic_url"   to  "사진 입니다"

            ))

            // 2. 이름없이 추가
            var i = play.add(mapOf(
                    "title"     to  "title 입니다. - ${Date().time} "
            ))

            // 2. 추가 후, id 값을 치환( unique key 처리)
            i.addOnSuccessListener {
                it.set(mapOf(
                        "title"     to  "title 입니다. - ${Date().time} ",
                        "music_url" to  "music 입니다",
                        "pic_url"   to  "사진 입니다",
                        "id"        to  it.id)
                )

                WriteLn(it.id)
            }


        }

        // Firestore의 비동기처리를 좀 편하게 ...
        val sm = StateMachine()
        btnFireStoreGet.setOnClickListener {
            sm.doStart()
        }

        btnUploadImage.setOnClickListener {
            // FileStorage 저장하기
            FileUpload()
        }

    }

    // 비동기를 동기적으로 처리하기 위한 클래스
    inner class  StateMachine {
        val myDB = FirebaseFirestore.getInstance()
        val play = myDB.collection("play")

        // 순서대로 진행할 함수(메소드)테이블
        val funcTable = listOf(::step1, ::step2, ::step3)
        var nIndx     = 0

        // 3. get list limited
        fun step3() {
            play.orderBy("title").limit(2).get().addOnSuccessListener {
                WriteLn("step 3 >> 검색된 갯수는 -> ${it.size()}")
                it.forEach { item ->
                    WriteLn(item.get("title") as String)
                }
            }
        }

        // 2. get list with where
        fun step2() {
            play.whereEqualTo("title", "문서가 하나만 존재함")
                    .get().addOnSuccessListener {
                        WriteLn("step 2 >> 검색결과는 -> ${it.size()}")

                        nextAction()
                    }
        }

        // 1. size & list
        fun step1() {
            play.get().addOnSuccessListener {
                WriteLn("step 1 >> ${it.size()}")
                it.forEach { item ->
                    WriteLn(item.get("title") as String)
                }

                nextAction()
            }
        }


        fun doStart(){
            // 비동기를 순서대로 동기처럼 실행하기
            nextAction()
        }

        // List의 확장함수
        fun nextAction(){
            // 크기가 넘어가면 초기화
            nIndx = if( nIndx > funcTable.size -1) 0 else nIndx
            val p = funcTable.get(nIndx) as () -> Unit
            p()
            nIndx++
        }

    }

    override fun onStart() {
        super.onStart()
        val currentUser = mAuth!!.getCurrentUser()
        WriteLn("User 정보: $currentUser")
    }

    // 사용자인증
    fun LoginUserInfo(email : String, password : String){

        mAuth!!.signInWithEmailAndPassword (email, password)
                .addOnCompleteListener(this) { task ->
                    if (task.isSuccessful) {
                        // Sign in success, update UI with the signed-in user's information
                        val user = mAuth!!.getCurrentUser()
                        WriteLn( "$user Login!.")
                    } else {
                        WriteLn( "Login failed")
                    }
                }
    }

    // 사용자추가
    fun CreateUserInfo(email : String, password : String){
        // create가 안된다면.. <- password 길이가 6자이상 필수!!
        mAuth!!.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(this) { task ->
                    if (task.isSuccessful) {
                        WriteLn("Create success")
                    } else {
                        WriteLn("Create failed")
                    }
                }
    }

    // file Upload
    fun FileUpload(){

        val storage = FirebaseStorage.getInstance()
        var storageRef = storage.reference

        var downUrl = ""

        // 언제나 절대경로명으로 만들어야 한다.
        var spaceRef = storageRef.child("images/space.jpg")

        fun Upload(){
            var uploading = spaceRef.putBytes(SaveImage())
            uploading.addOnFailureListener {

            }.addOnSuccessListener {
                WriteLn("Uploaded ")

                addImageWithLibrary(downUrl, { ctx, url, image ->
                    // Glide로 추가
                    Glide.with(ctx)
                            .load(url)
                            .fitCenter()
                            .into(image)
                })
            }
        }

        // 올라간 주소를 가져온다.
        spaceRef.downloadUrl.addOnSuccessListener {
            WriteLn(it.toString())
            downUrl = it.toString()
            Upload()
        }

    }

    // 함수형 프로그래밍 스타일
    private fun addImageWithLibrary(
            imgUrl: String, func: (Context, String, ImageView) -> Unit) {

        // 넘겨진 함수를 수행
        func(applicationContext, imgUrl, imgDownload)
    }

    // 2. TextClock 화면을 File로 저장하는 메소드 구현
    fun SaveImage(): ByteArray {

        clock.isDrawingCacheEnabled = true
        clock.buildDrawingCache(true)

        val b = Bitmap.createBitmap(clock.drawingCache)

        clock.isDrawingCacheEnabled = false
        clock.buildDrawingCache(false)

        var bs = ByteArrayOutputStream()
        b.compress(Bitmap.CompressFormat.PNG, 85, bs)
        return bs.toByteArray()
    }

    var nCount = 0
    fun Write(s : String ){
        val readString = txtMessage.text.toString()
        txtMessage.text = "${readString}${s}"
    }

    fun WriteLn(s : String ){
        Write("${nCount++}> ")
        Write( "$s\n" )
    }
}
