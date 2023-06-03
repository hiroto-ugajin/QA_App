package jp.techacademy.hiroto.ugajin.qa_app

import android.content.Intent
import android.os.Bundle
import com.google.android.material.snackbar.Snackbar
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.navigateUp
import androidx.navigation.ui.setupActionBarWithNavController
import android.view.Menu
import android.view.MenuItem
import com.google.firebase.auth.FirebaseAuth
import jp.techacademy.hiroto.ugajin.qa_app.databinding.ActivityMainBinding
import androidx.appcompat.app.ActionBarDrawerToggle // 追加
import androidx.core.view.GravityCompat // 追加
import com.google.android.material.navigation.NavigationView // 追加

class MainActivity : AppCompatActivity(), NavigationView.OnNavigationItemSelectedListener {

//    private lateinit var appBarConfiguration: AppBarConfiguration
    private lateinit var binding: ActivityMainBinding
    private var genre = 0  // 追加

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setSupportActionBar(binding.content.toolbar)

        // ----- 修正:ここから
        binding.content.fab.setOnClickListener {
            // ジャンルを選択していない場合はメッセージを表示するだけ
            if (genre == 0) {
                Snackbar.make(it, getString(R.string.question_no_select_genre), Snackbar.LENGTH_LONG).show()
                return@setOnClickListener
            }

            // ログイン済みのユーザーを取得する
            val user = FirebaseAuth.getInstance().currentUser

            // ログインしていなければログイン画面に遷移させる
            if (user == null) {
                val intent = Intent(applicationContext, LoginActivity::class.java)
                startActivity(intent)
            } else {
                // ジャンルを渡して質問作成画面を起動する
                val intent = Intent(applicationContext, QuestionSendActivity::class.java)
                intent.putExtra("genre", genre)
                startActivity(intent)
            }
        }
        // ----- 修正:ここまで

        // ナビゲーションドロワーの設定
        val toggle = ActionBarDrawerToggle(
            this,
            binding.drawerLayout,
            binding.content.toolbar,
            R.string.app_name,
            R.string.app_name
        )
        binding.drawerLayout.addDrawerListener(toggle)
        toggle.syncState()

        binding.navView.setNavigationItemSelectedListener(this)
        // ----- 追加:ここまで
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        // Inflate the menu; this adds items to the action bar if it is present.
        menuInflater.inflate(R.menu.menu_main, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
//        // Handle action bar item clicks here. The action bar will
//        // automatically handle clicks on the Home/Up button, so long
//        // as you specify a parent activity in AndroidManifest.xml.
//        return when (item.itemId) {
//            R.id.action_settings -> true
//            else -> super.onOptionsItemSelected(item)
//        }
        val id = item.itemId

        if (id == R.id.action_settings) {
            val intent = Intent(applicationContext, SettingActivity::class.java)
            startActivity(intent)
            return true
        }
        return super.onOptionsItemSelected(item)
    }

    // ----- 追加:ここから
    override fun onNavigationItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.nav_hobby -> {
                binding.content.toolbar.title = getString(R.string.menu_hobby_label)
                genre = 1
            }
            R.id.nav_life -> {
                binding.content.toolbar.title = getString(R.string.menu_life_label)
                genre = 2
            }
            R.id.nav_health -> {
                binding.content.toolbar.title = getString(R.string.menu_health_label)
                genre = 3
            }
            R.id.nav_computer -> {
                binding.content.toolbar.title = getString(R.string.menu_computer_label)
                genre = 4
            }
        }

        binding.drawerLayout.closeDrawer(GravityCompat.START)
        return true
    }
    // ----- 追加:ここまで
}