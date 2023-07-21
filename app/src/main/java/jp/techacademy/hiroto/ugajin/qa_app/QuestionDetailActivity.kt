package jp.techacademy.hiroto.ugajin.qa_app

import android.content.Intent
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import android.view.View // 課題対応:追加
import androidx.core.content.ContextCompat // 課題対応:追加
import jp.techacademy.hiroto.ugajin.qa_app.databinding.ActivityQuestionDetailBinding


class QuestionDetailActivity : AppCompatActivity(), View.OnClickListener {
    private lateinit var binding: ActivityQuestionDetailBinding

    private lateinit var question: Question
    private lateinit var adapter: QuestionDetailListAdapter
    private lateinit var answerRef: DatabaseReference

    // ----- 課題対応:ここから
    private var isFavorite: Boolean = false
    // ----- 課題対応:ここまで

    private val eventListener = object : ChildEventListener {
        override fun onChildAdded(dataSnapshot: DataSnapshot, s: String?) {
            val map = dataSnapshot.value as Map<*, *>

            val answerUid = dataSnapshot.key ?: ""

            for (answer in question.answers) {
                // 同じAnswerUidのものが存在しているときは何もしない
                if (answerUid == answer.answerUid) {
                    return
                }
            }

            val body = map["body"] as? String ?: ""
            val name = map["name"] as? String ?: ""
            val uid = map["uid"] as? String ?: ""

            val answer = Answer(body, name, uid, answerUid)
            question.answers.add(answer)
            adapter.notifyDataSetChanged()
        }

        override fun onChildChanged(dataSnapshot: DataSnapshot, s: String?) {}
        override fun onChildRemoved(dataSnapshot: DataSnapshot) {}
        override fun onChildMoved(dataSnapshot: DataSnapshot, s: String?) {}
        override fun onCancelled(databaseError: DatabaseError) {}
    }

    // ----- 課題対応:ここから
    private val favoriteListener = object : ChildEventListener {
        override fun onChildAdded(dataSnapshot: DataSnapshot, s: String?) {
            binding.favoriteButton.text = getString(R.string.question_favorite_disable)
            val color = ContextCompat.getColor(applicationContext, R.color.orange_500)
            binding.favoriteButton.setBackgroundColor(color)
            isFavorite = true
        }

        override fun onChildChanged(dataSnapshot: DataSnapshot, s: String?) {}
        override fun onChildRemoved(dataSnapshot: DataSnapshot) {}
        override fun onChildMoved(dataSnapshot: DataSnapshot, s: String?) {}
        override fun onCancelled(databaseError: DatabaseError) {}
    }
    // ----- 課題対応:ここまで

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityQuestionDetailBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // 渡ってきたQuestionのオブジェクトを保持する
        // API33以上でgetSerializableExtra(key)が非推奨となったため処理を分岐
        @Suppress("UNCHECKED_CAST", "DEPRECATION", "DEPRECATED_SYNTAX_WITH_DEFINITELY_NOT_NULL")
        question = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU)
            intent.getSerializableExtra("question", Question::class.java)!!
        else
            intent.getSerializableExtra("question") as? Question!!

        title = question.title

        // ListViewの準備
        adapter = QuestionDetailListAdapter(this, question)
        binding.listView.adapter = adapter
        adapter.notifyDataSetChanged()

        binding.fab.setOnClickListener {
            // ログイン済みのユーザーを取得する
            val user = FirebaseAuth.getInstance().currentUser

            if (user == null) {
                // ログインしていなければログイン画面に遷移させる
                val intent = Intent(applicationContext, LoginActivity::class.java)
                startActivity(intent)
            } else {
                // Questionを渡して回答作成画面を起動する
                // --- ここから ---
                val intent = Intent(applicationContext, AnswerSendActivity::class.java)
                intent.putExtra("question", question)
                startActivity(intent)
                // --- ここまで ---
            }
        }

        val dataBaseReference = FirebaseDatabase.getInstance().reference
        answerRef = dataBaseReference.child(ContentsPATH).child(question.genre.toString())
            .child(question.questionUid).child(AnswersPATH)
        answerRef.addChildEventListener(eventListener)

        // ----- 課題対応:ここから
        // ログインしていなければ、お気に入りボタンを非表示にする
        binding.favoriteButton.setOnClickListener(this)
        val user = FirebaseAuth.getInstance().currentUser
        if (user == null) {
            binding.favoriteButton.visibility = View.INVISIBLE
        } else {
            // 初期状態は未登録扱いにしておく
            binding.favoriteButton.text = getString(R.string.question_favorite_enable)
            val color = ContextCompat.getColor(applicationContext, android.R.color.darker_gray)
            binding.favoriteButton.setBackgroundColor(color)
            isFavorite = false

            // お気に入りに登録済みかチェック
            val databaseReference = FirebaseDatabase.getInstance().reference
            val favoriteRef =
                databaseReference.child(FavoritePATH).child(user.uid).child(question.questionUid)
            favoriteRef.addChildEventListener(favoriteListener)
        }
        // ----- 課題対応:ここまで
    }


    // ----- 課題対応:ここから
    override fun onClick(view: View) {
        //ログイン済みのユーザーを取得する
        val user = FirebaseAuth.getInstance().currentUser

        if (view.id == R.id.favoriteButton) {
            val databaseReference = FirebaseDatabase.getInstance().reference
            val favoriteRef =
                databaseReference.child(FavoritePATH).child(user!!.uid).child(question.questionUid)

            if (isFavorite) {
                // お気に入りに登録済み->解除
                favoriteRef.removeValue()

                binding.favoriteButton.text = getString(R.string.question_favorite_enable)
                val color = ContextCompat.getColor(applicationContext, android.R.color.darker_gray)
                binding.favoriteButton.setBackgroundColor(color)
                isFavorite = false
                //favoriteQuestionUidList.remove(question.questionUid)

            } else {
                // お気に入りに登録
                val favorite = HashMap<String, String>()
                favorite["genre"] = question.genre.toString()
                favoriteRef.setValue(favorite)

                binding.favoriteButton.text = getString(R.string.question_favorite_disable)
                val color = ContextCompat.getColor(applicationContext, R.color.orange_500)
                binding.favoriteButton.setBackgroundColor(color)
                isFavorite = true
                //favoriteQuestionUidList.add(question.questionUid)
            }
        }
    }
    // ----- 課題対応:ここまで
}