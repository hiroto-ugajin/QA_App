package jp.techacademy.hiroto.ugajin.qa_app

// Firebaseにユーザの表示名を保存するパス
const val UsersPATH = "users"

// Firebaseに質問を保存するバス
const val ContentsPATH = "contents"

// Firebaseに回答を保存するパス
const val AnswersPATH = "answers"

// Preferenceに表示名を保存する時のキー
const val NameKEY = "name"

// ----- 課題対応:ここから
const val FavoritePATH = "favorite" // Firebaseにお気に入りの質問を保存するパス
var favoriteQuestionUidList = arrayListOf<String>()    // お気に入り一覧を保存するリスト
// ----- 課題対応:ここまで
