#### Firebase Quick Example
> Firebase가 빠르게 성장하다보니 버전간 호환성에 많은 고민을 해야한다.

주의
1. 되도록이면 Android Studio에서 제공하는 Tool보다는 Firebase 사이트에서 직접 수동으로 진행할 것. 버전 충돌이 종종 일어남.
2. 인터넷 예제들은 Firebase 모듈간 호환성도 고려해야 함. Gradle의 버전설정 민감함

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


