package ca.TransCanadaTrail.TheGreatTrail.adapters;

import android.content.Context;
import android.content.Intent;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import ca.TransCanadaTrail.TheGreatTrail.R;
import ca.TransCanadaTrail.TheGreatTrail.activities.AchievementDetailsActivity;
import ca.TransCanadaTrail.TheGreatTrail.models.Achievement;

/**
 * Created by Islam Salah on 7/12/17.
 */

public class AchievementsAdapter extends RecyclerView.Adapter<AchievementsAdapter.AchievementVH> {

    private Context context;
    private List<Achievement> achievements;


    public AchievementsAdapter(Context context, List<Achievement> achievements) {
        this.context = context;
        this.achievements = achievements;
    }

    @Override
    public AchievementVH onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.list_item_achievement, parent, false);
        return new AchievementVH(view);
    }

    @Override
    public void onBindViewHolder(AchievementVH holder, int position) {
        Achievement achievement = achievements.get(position);
        holder.bind(achievement);
    }

    @Override
    public int getItemCount() {
        return achievements == null ? 0 : achievements.size();
    }

    public class AchievementVH extends RecyclerView.ViewHolder {

        @BindView(R.id.list_item_achievement_iv)
        ImageView achievementIV;

        @BindView(R.id.list_item_achievement_title_tv)
        TextView achievementTitleTV;

        private Achievement achievement;

        public AchievementVH(View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);
        }

        private void bind(Achievement achievement) {
            this.achievement = achievement;
            achievementIV.setImageDrawable(achievement.getAchievementImage(context));
            achievementTitleTV.setText(achievement.getAchievementTitle(context));
        }

        @OnClick(R.id.list_item_achievement)
        void showDetails() {
            Intent intent = AchievementDetailsActivity.newIntent(context, achievement.getId());
            context.startActivity(intent);
        }
    }
}
