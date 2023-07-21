package jp.techacademy.hiroto.ugajin.qa_app

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Base64
import android.view.MenuItem
import android.widget.AdapterView
import androidx.core.app.NavUtils
import com.google.firebase.database.*
import jp.techacademy.hiroto.ugajin.qa_app.databinding.ActivityFavoriteBinding
import java.util.ArrayList
import java.util.HashMap




class FavoriteActivity: AppCompatActivity() {
    private lateinit var binding: ActivityFavoriteBinding

    private lateinit var databaseReference: DatabaseReference
    private lateinit var favGenreRef: DatabaseReference
    private lateinit var questionArrayList: ArrayList<Question>
    private lateinit var adapter: QuestionsListAdapter

    private val eventListener = object : ChildEventListener {
        override fun onChildAdded(dataSnapshot: DataSnapshot, s: String?) {
            // ジャンルごとにまず取れるので、それを１つずつ見て中身を抽出する
            val map = dataSnapshot.value as? HashMap<*, *>
            if (map != null) {
                for ((key, value) in map) {
                    val map2 = value as Map<*, *>
                    val title = map2["title"] as? String ?: ""
                    val body = map2["body"] as? String ?: ""
                    val name = map2["name"] as? String ?: ""
                    val uid = map2["uid"] as? String ?: ""
                    val imageString = map2["image"] as? String ?: ""
                    val bytes =
                        if (imageString.isNotEmpty()) {
                            Base64.decode(imageString, Base64.DEFAULT)
                        } else {
                            byteArrayOf()
                        }

                    val answerArrayList = ArrayList<Answer>()
                    val answerMap = map2["answers"] as Map<*, *>?
                    if (answerMap != null) {
                        for (key1 in answerMap.keys) {
                            val map1 = answerMap[key1] as Map<*, *>
                            val map1Body = map1["body"] as? String ?: ""
                            val map1Name = map1["name"] as? String ?: ""
                            val map1Uid = map1["uid"] as? String ?: ""
                            val map1AnswerUid = key1 as? String ?: ""
                            val answer = Answer(map1Body, map1Name, map1Uid, map1AnswerUid)
                            answerArrayList.add(answer)
                        }
                    }

                    // ジャンルはdataSnapshot.keyとなる
                    val genre = (dataSnapshot.key as? String ?: "").toIntOrNull() ?: 0

                    val question = Question(
                        title, body, name, uid, key as? String ?: "",
                        genre, bytes, answerArrayList
                    )

                    if (favoriteQuestionUidList.contains(question.questionUid)) {
                        questionArrayList.add(question)
                    }
                }
            }
            adapter.notifyDataSetChanged()
        }

        override fun onChildChanged(dataSnapshot: DataSnapshot, s: String?) {
            val map = dataSnapshot.value as Map<*, *>

            //変更があったQuestionを探す
            for (question in questionArrayList) {
                if (dataSnapshot.key == question.questionUid) {
                    //このアプリで変更がある可能性があるのは回答(Answer)のみ
                    question.answers.clear()
                    val answerMap = map["answers"] as? Map<*, *>
                    if (answerMap != null) {
                        for (key in answerMap.keys) {
                            val temp = answerMap[key] as HashMap<*, *>
                            val answerBody = temp["body"] as? String ?: ""
                            val answerName = temp["name"] as? String ?: ""
                            val answerUid = temp["uid"] as? String ?: ""
                            val answer = Answer(answerBody, answerName, answerUid, key as String)
                            question.answers.add(answer)
                        }
                    }
                    adapter.notifyDataSetChanged()
                }
            }
        }

        override fun onChildRemoved(dataSnapshot: DataSnapshot) {}
        override fun onChildMoved(dataSnapshot: DataSnapshot, s: String?) {}
        override fun onCancelled(databaseError: DatabaseError) {}
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityFavoriteBinding.inflate(layoutInflater)
        setContentView(binding.root)

        title = getString(R.string.favorite_title)

        supportActionBar!!.setDisplayHomeAsUpEnabled(true)

        databaseReference = FirebaseDatabase.getInstance().reference
        favGenreRef = databaseReference.child(ContentsPATH)

        adapter = QuestionsListAdapter(this)
        questionArrayList = ArrayList()

        binding.listView.onItemClickListener =
            AdapterView.OnItemClickListener { _, _, position, _ ->
                // Questionのインスタンスを渡して質問詳細画面を起動する
                val intent = Intent(applicationContext, QuestionDetailActivity::class.java)
                intent.putExtra("question", questionArrayList[position])
                startActivity(intent)
            }
    }

    override fun onResume() {
        super.onResume()

        favGenreRef.addChildEventListener(eventListener)
        questionArrayList.clear()
        adapter.notifyDataSetChanged()
        adapter.setQuestionArrayList(questionArrayList)
        binding.listView.adapter = adapter
    }
}







