package com.xiaoyv.common.widget.progress

import android.content.Context
import android.util.AttributeSet
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.view.isVisible
import com.xiaoyv.common.api.parser.entity.MediaDetailEntity
import com.xiaoyv.common.config.annotation.InterestType
import com.xiaoyv.common.config.annotation.MediaType
import com.xiaoyv.common.databinding.ViewEpProgressBinding
import com.xiaoyv.common.kts.inflater
import com.xiaoyv.widget.callback.setOnFastLimitClickListener

/**
 * Class: [AnimeEpProgressView]
 *
 * @author why
 * @since 12/23/23
 */
class AnimeEpProgressView @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null,
) : ConstraintLayout(context, attrs) {
    private val binding = ViewEpProgressBinding.inflate(context.inflater, this)

    private val MediaDetailEntity.totalProgressTip
        get() = if (progressMax == 0) "*" else progressMax.toString()

    private val MediaDetailEntity.totalProgressSecondTip
        get() = if (progressSecondMax == 0) "*" else progressSecondMax.toString()

    /**
     * 绑定
     *
     * @param epOrVol 是否为第一个进度条
     */
    fun bind(
        entity: MediaDetailEntity,
        epOrVol: Boolean,
        clickAddListener: (MediaDetailEntity, Boolean) -> Unit,
    ) {
        // 条目类型是否支持章节进度
        val canEditEpProgress = MediaType.canEditEpProgress(entity.mediaType)
        if (canEditEpProgress.not()) {
            isVisible = false
            return
        }

        // 收藏类型是否满足进度条显示条件
        isVisible = entity.collectState.interest != InterestType.TYPE_UNKNOWN
        if (isVisible.not()) return

        // Vol的进度条，仅书籍能显示
        if (isVisible && !epOrVol) {
            isVisible = entity.mediaType == MediaType.TYPE_BOOK
            if (isVisible.not()) return
        }

        val progressTip = when {
            entity.mediaType == MediaType.TYPE_BOOK && epOrVol -> "Chap"
            entity.mediaType == MediaType.TYPE_BOOK -> "Vol"
            else -> "我的完成度"
        }

        // 书籍的VOL进度条
        if (entity.mediaType == MediaType.TYPE_BOOK && epOrVol.not()) {
            binding.tvProgress.text = buildString {
                append(progressTip)
                append("：")
                append(entity.progressSecond)
                append(" / ")
                append(entity.totalProgressSecondTip)
            }
            binding.ivAdd.isVisible = (entity.progressSecond != entity.progressSecondMax
                    || entity.progressSecondMax == 0)
            updatePanelProgress(entity.progressSecond, entity.progressSecondMax)
        }
        // EP 进度条
        else {
            binding.tvProgress.text = buildString {
                append(progressTip)
                append("：")
                append(entity.progress)
                append(" / ")
                append(entity.totalProgressTip)
            }
            binding.ivAdd.isVisible = (entity.progress != entity.progressMax
                    || entity.progressMax == 0)
            updatePanelProgress(entity.progress, entity.progressMax)
        }

        binding.ivAdd.setOnFastLimitClickListener {
            clickAddListener(entity, epOrVol)
        }
    }

    private fun updatePanelProgress(progress: Int, max: Int) {
        binding.progressPanel.post {
            val percent = if (max <= 0) 0f else progress.toFloat() / max
            val fixedPercent = percent.coerceIn(0f, 1f)
            val fillWidth = (binding.progressPanel.width * fixedPercent).toInt()
            binding.progressFill.isVisible = fillWidth > 0
            binding.progressFill.layoutParams = binding.progressFill.layoutParams.apply {
                width = fillWidth
            }
        }
    }
}
