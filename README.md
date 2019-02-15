#### Firebase Quick Example
> Firebase를 빠르게 적용하기 

주의
1. 되도록이면 Android Studio에서 제공하는 Tool보다는 Firebase 사이트에서 직접 수동으로 진행할 것. 버전 충돌이 종 일어남.
2. 인터넷 예제들은 Firebase 모듈간 호환성도 고려해야 함. Gradle의 버전설정 민감함
3. app 폴더 및의 google-services.json은 다음과 같이 새롭게 만들어 사용해야 한다(보안문제).
    1. 자신의 Firebase 개발자 콘솔로 접속.
    2. 프로젝트 생성.
    3. 설정 메뉴 이동 후,
    4. google-services.json 다운로드
    5. 지금 프로젝트의 app 폴더에 복사

app 폴더의 build.gradle

~~~

    implementation 'com.google.firebase:firebase-auth:16.0.2'
    implementation 'com.google.firebase:firebase-database:16.0.2'
    implementation 'com.google.firebase:firebase-firestore:16.0.0'
    implementation "com.google.firebase:firebase-storage:16.0.2"

~~~

- Firebase auth
- Firebase RealTime Database
- Firebase FireStore
- Firebase FireStorage


