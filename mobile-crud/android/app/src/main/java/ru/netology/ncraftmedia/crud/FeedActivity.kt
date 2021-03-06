package ru.netology.ncraftmedia.crud

import android.app.ProgressDialog
import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import kotlinx.android.synthetic.main.activity_feed.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.launch
import ru.netology.ncraftmedia.R
import ru.netology.ncraftmedia.crud.adapter.PostAdapter
import ru.netology.ncraftmedia.crud.dto.PostModel

class FeedActivity : AppCompatActivity(), CoroutineScope by MainScope(),
  PostAdapter.OnLikeBtnClickListener {
  private var dialog: ProgressDialog? = null

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    setContentView(R.layout.activity_feed)
    fab.setOnClickListener {
      startActivity(
        Intent(this, CreatePostActivity::class.java)
      )
    }
  }

  override fun onStart() {
    super.onStart()
    launch {
      dialog = ProgressDialog(this@FeedActivity).apply {
        setMessage(this@FeedActivity.getString(R.string.please_wait))
        setTitle(R.string.downloading_posts)
        setCancelable(false)
        setProgressBarIndeterminate(true)
        show()
      }
      val result = Repository.getPosts()
      dialog?.dismiss()
      if (result.isSuccessful) {
        with(container) {
          layoutManager = LinearLayoutManager(this@FeedActivity)
          adapter = PostAdapter(result.body() ?: emptyList()).apply {
            likeBtnClickListener = this@FeedActivity
          }
        }
      } else {
        Toast.makeText(this@FeedActivity, R.string.error_occured, Toast.LENGTH_SHORT)
      }
    }
  }

  override fun onLikeBtnClicked(item: PostModel, position: Int) {
    launch {
      item.likeActionPerforming = true
      with(container) {
        adapter?.notifyItemChanged(position)
        val response = if (item.likedByMe) {
          Repository.cancelMyLike(item.id)
        } else {
          Repository.likedByMe(item.id)
        }
        item.likeActionPerforming = false
        if (response.isSuccessful) {
          item.updateLikes(response.body()!!)
        }
        adapter?.notifyItemChanged(position)
      }
    }
  }
}
