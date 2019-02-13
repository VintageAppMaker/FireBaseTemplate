package com.psw.adsloader.firebasetemplate

import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.android.synthetic.main.activity_main.*
import java.util.*

class MainActivity : AppCompatActivity() {

    var  mAuth : FirebaseAuth? = null

    data class Item(var name : String, var img : String, var mp3 : String )
    private val posts: MutableList<Item> = mutableListOf()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        mAuth = FirebaseAuth.getInstance()

        txtLogin.setOnClickListener {
            AddUserInfo("snake2@caver.com", "123456mode6")
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

        btnFireStoreGet.setOnClickListener {
            val myDB = FirebaseFirestore.getInstance()
            val play = myDB.collection("play")

            fun List<Any>?.nextAction(cnt : Int){
                val p = this?.get(cnt) as (Int, List <Any>) -> Unit
                p(cnt + 1, this)
            }

            // 3. get list limited
            fun step3(cnt : Int, lst : List<Any>) {
                play.orderBy("title").limit(2).get().addOnSuccessListener {
                    WriteLn("step 3 >> 검색된 갯수는 -> ${it.size()}")
                    it.forEach { item ->
                        WriteLn(item.get("title") as String)
                    }
                }
            }

            // 2. get list with where
            fun step2(cnt : Int, lst : List<Any>) {
                play.whereEqualTo("title", "문서가 하나만 존재함")
                        .get().addOnSuccessListener {
                            WriteLn("step 2 >> 검색결과는 -> ${it.size()}")

                            lst.nextAction(cnt)
                        }
            }

            // 1. size & list
            fun step1(cnt : Int, lst : List<Any>) {
                play.get().addOnSuccessListener {
                    WriteLn("step 1 >> ${it.size()}")
                    it.forEach { item ->
                        WriteLn(item.get("title") as String)
                    }

                    lst.nextAction(cnt)
                }
            }

            // 비동기를 순서대로 동기처럼 실행하기
            listOf(::step1, ::step2, ::step3).let{
                // 첫번째 인자가 시작함수
                if(it.size > 1) it.get(0)(1, it)
            }

        }

    }

    override fun onStart() {
        super.onStart()
        val currentUser = mAuth!!.getCurrentUser()
        WriteLn("User 정보: $currentUser")
    }

    // 사용자인증
    fun AddUserInfo(email : String, password : String){

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
