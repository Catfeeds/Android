package ebag.hd.widget.questions;

import android.content.Context;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.view.ViewGroup;

import java.util.Arrays;
import java.util.List;

import ebag.core.bean.QuestionBean;
import ebag.hd.widget.questions.base.BaseQuestionView;
import ebag.hd.widget.questions.base.FillBlankView;

/**
 * Created by unicho on 2017/12/23.
 */

public class CompleteView extends BaseQuestionView {
    private List<String> title;
    /**
     * 题目内容
     */
    private FillBlankView fillBlankView;
    private String questionContent;
    private String rightAnswer;
    private String studentAnswer;
    public CompleteView(Context context) {
        super(context);
    }

    public CompleteView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
    }

    public CompleteView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @Override
    protected void addBody(Context context) {
        fillBlankView = new FillBlankView(context);
        LayoutParams fillBlankParams = new LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);

        addView(fillBlankView,fillBlankParams);
    }

    @Override
    public void setData(QuestionBean questionBean) {
        title = Arrays.asList(questionBean.getQuestionHead().split("#R#"));
        questionContent = questionBean.getQuestionContent();

        studentAnswer = questionBean.getAnswer();
        rightAnswer = questionBean.getRightAnswer();
    }

    @Override
    public void show(boolean active) {
        setTitle(title);
        fillBlankView.setActive(active);
        fillBlankView.setData(questionContent);
    }

    @Override
    public void questionActive(boolean active) {
        fillBlankView.setActive(active);
    }

    @Override
    public boolean isQuestionActive() {
        return fillBlankView.isActive();
    }

    @Override
    public void showResult() {
        show(false);
        fillBlankView.showResult(studentAnswer,rightAnswer);
    }

    @Override
    public String getAnswer() {
        return fillBlankView.getAnswer("#R#");
    }

    @Override
    public void reset() {
        questionActive(true);
        fillBlankView.setData(questionContent);
    }
}
