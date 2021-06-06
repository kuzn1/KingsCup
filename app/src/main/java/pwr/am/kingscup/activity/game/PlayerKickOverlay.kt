package pwr.am.kingscup.activity.game

import android.app.Activity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import pwr.am.kingscup.databinding.PlayerViewRowBinding
import pwr.am.kingscup.databinding.PlayetChooseViewBinding

class PlayerKickOverlay(private val context : Activity, gameKey: String) {

    private var binding = PlayetChooseViewBinding.inflate(LayoutInflater.from(context))
    private var referencePlayers = Firebase.database.getReference("games/$gameKey/players")

    fun show() {
        referencePlayers.get().addOnSuccessListener {
            binding.root.setOnClickListener {
                context.runOnUiThread {
                    if (binding.root.parent != null)
                        (binding.root.parent as ViewGroup).removeView(binding.root)
                }
            }
            binding.playerList.removeAllViews()
            for (player in it.children) {
                if(player.child("name").value != null && player.key != null) {
                    val row = PlayerViewRowBinding.inflate(LayoutInflater.from(context))
                    row.nickName.text = player.child("name").value as String
                    row.nickName.textSize = 18f
                    row.kickButton.visibility = View.VISIBLE
                    row.kickButton.setOnClickListener {
                        referencePlayers.child(player.key as String).removeValue()
                        context.runOnUiThread {
                            if (binding.root.parent != null)
                                (binding.root.parent as ViewGroup).removeView(binding.root)
                        }
                    }
                    binding.playerList.addView(row.root)
                }
            }
            context.runOnUiThread{
                context.addContentView(
                    binding.root,
                    ViewGroup.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT,
                        ViewGroup.LayoutParams.MATCH_PARENT
                    )
                )
            }
        }
    }
}