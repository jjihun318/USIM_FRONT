// LockedBadgeAdapter.kt
package com.pack.info_2

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ProgressBar
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView



class LockedBadgeAdapter(private var badges: List<Badge>) :
    RecyclerView.Adapter<LockedBadgeAdapter.BadgeViewHolder>() {

    // 뷰 홀더: badge_1.xml 레이아웃의 요소를 연결합니다.
    class BadgeViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val title: TextView = view.findViewById(R.id.badge_title_1)
        val description: TextView = view.findViewById(R.id.badge_description_1)
        val detail: TextView = view.findViewById(R.id.badge_detail_1)
        val progressText: TextView = view.findViewById(R.id.badge_progress_1)
        val progressBar: ProgressBar = view.findViewById(R.id.badge_progress_bar_1)
    }

    // 뷰 홀더 생성: 템플릿(badge_1.xml)을 인플레이트합니다.
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BadgeViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.badge_1, parent, false)
        return BadgeViewHolder(view)
    }

    // 데이터 바인딩: 각 위치의 데이터를 뷰 홀더에 설정합니다.
    override fun onBindViewHolder(holder: BadgeViewHolder, position: Int) {
        val badge = badges[position]
        holder.title.text = badge.missionName
        holder.description.text = badge.missionDescription
        holder.detail.text = badge.missionDetail
        holder.progressText.text = badge.progressStatus

        holder.progressBar.max = 100 // 최대값은 100으로 고정한다고 가정
        holder.progressBar.progress = badge.gaugeRatio
    }

    // 아이템 개수 반환
    override fun getItemCount() = badges.size

    // 데이터 업데이트 함수 (옵션)
    fun updateBadges(newBadges: List<Badge>) {
        this.badges = newBadges
        notifyDataSetChanged()
    }
}