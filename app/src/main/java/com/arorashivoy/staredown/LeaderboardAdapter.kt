package com.arorashivoy.staredown

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.cardview.widget.CardView
import androidx.recyclerview.widget.RecyclerView

class LeaderboardAdapter(private val leaders: List<Leader>) : RecyclerView.Adapter<LeaderboardAdapter.LeaderViewHolder>() {

    inner class LeaderViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val username: TextView = view.findViewById(R.id.username)
        val score: TextView = view.findViewById(R.id.score)
        val medalIcon: ImageView = view.findViewById(R.id.medalIcon)
        val rankCircle: CardView = view.findViewById(R.id.rankCircle)
        val rankNumber: TextView = view.findViewById(R.id.rankNumber)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): LeaderViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_leaderboard_card, parent, false)
        return LeaderViewHolder(view)
    }

    override fun onBindViewHolder(holder: LeaderViewHolder, position: Int) {
        val leader = leaders[position]
        holder.username.text = leader.username

        // Convert score in milliseconds to hh:mm:ss
        val totalMillis = leader.score
        val minutes = totalMillis / 60000
        val seconds = (totalMillis % 60000) / 1000
        val millis = totalMillis % 1000
        holder.score.text = String.format("%02d:%02d:%03d", minutes, seconds, millis)

        // Top 3 get medals
        when (position) {
            0 -> {
                holder.medalIcon.visibility = View.VISIBLE
                holder.medalIcon.setImageResource(R.drawable.goldmedal)
                holder.rankCircle.visibility = View.GONE
            }
            1 -> {
                holder.medalIcon.visibility = View.VISIBLE
                holder.medalIcon.setImageResource(R.drawable.silvermedal)
                holder.rankCircle.visibility = View.GONE
            }
            2 -> {
                holder.medalIcon.visibility = View.VISIBLE
                holder.medalIcon.setImageResource(R.drawable.bronzemedal)
                holder.rankCircle.visibility = View.GONE
            }
            else -> {
                holder.medalIcon.visibility = View.GONE
                holder.rankCircle.visibility = View.VISIBLE
                holder.rankNumber.text = (position + 1).toString()
            }
        }
    }

    override fun getItemCount(): Int = leaders.size
}
