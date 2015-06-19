package com.stone.textview.animation;

import java.util.ArrayList;
import java.util.List;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Paint.Align;
import android.graphics.Rect;
import android.os.Handler;
import android.os.Message;
import android.util.AttributeSet;
import android.view.View;

@SuppressLint("HandlerLeak")
public class AnimTextView extends View {

	private String showText;
	private Paint paint;
	private List<CharHolder> charList = new ArrayList<CharHolder>();
	private int cycleNum = 0; // 线程循环，sleep的次数
	private int maxDelayNum = 16; // 每个文字delay的线程循环次数是随机的，这个值是随机函数最大值的种子
	private int finishCycleNum = 8; // 从透明到完全不透明的线程循环次数

	// 在onDraw函数中调用canvas.drawText需要传入x/y坐标，那个坐标是文字左下角的坐标。
	private int firstLineOffset = 0;

	// drawText的y初始值非常诡谲，根据stackoverflow的反馈，那个y值要通过计算单个文字占据的高度，才比较科学、合理，但是存在一定的偏差
	// 这个extraPaddingTop就是用来弥补偏差的，不代表绝对正确
	private int extraPaddingTop = PixValue.dip.valueOf(1f);

	// 以下属性需要通过xml文件来配置
	private int textSize = PixValue.sp.valueOf(14); // 字号
	private int textColor = Color.WHITE; // 文字颜色
	private int lineSpaceExtra = 10; // 行距

	public AnimTextView(Context context) {
		this(context, null);
	}

	public AnimTextView(Context context, AttributeSet attrs) {
		this(context, attrs, 0);
	}

	public AnimTextView(Context context, AttributeSet attrs, int defStyleAttr) {
		super(context, attrs, defStyleAttr);

		// 1. 初始化显示文本、字号、颜色、行距等信息
		TypedArray typedArray = context.obtainStyledAttributes(attrs,
				R.styleable.textAnim);
		showText = typedArray.getString(R.styleable.textAnim_showText);
		textSize = typedArray.getDimensionPixelSize(
				R.styleable.textAnim_textSize, textSize);
		textColor = typedArray.getColor(R.styleable.textAnim_textColor,
				textColor);
		lineSpaceExtra = typedArray.getDimensionPixelSize(
				R.styleable.textAnim_lineSpaceExtra, lineSpaceExtra);
		typedArray.recycle();

		// 2. 初始化paint画笔
		paint = new Paint();
		paint.setAntiAlias(true);
		paint.setColor(textColor);
		paint.setTextSize(textSize);
		paint.setTextAlign(Align.LEFT);

		// 3. 初始化那个诡谲的y坐标值
		Rect rect = new Rect();
		paint.getTextBounds("豆", 0, 1, rect); // 以一个典型的汉字为模板，计算高度
		firstLineOffset = (int) (rect.height() - rect.bottom) + extraPaddingTop; // stackoverflow上面别人给的建议

		// 3. 初始化字符串
		initCharList();
	}

	private void initCharList() {
		// 如果显示文本为空，则不显示
		if (showText == null || showText.length() == 0) {
			return;
		}

		charList.clear();

		int length = showText.length();
		for (int i = 0; i < length; i++) {
			CharHolder charItem = new CharHolder();
			charItem.charTxt = "" + showText.charAt(i);

			if ("\n".equals(charItem.charTxt)) {
				// 换行、不需要alpha渐变
				charItem.initDelay = Integer.MAX_VALUE;
				charItem.measureWidth = 0;
				charList.add(charItem);
				continue;
			}

			charItem.measureWidth = (int) paint.measureText(charItem.charTxt);

			if (" ".equals(charItem.charTxt)) {
				// 空格不需要alpha渐变
				charItem.initDelay = Integer.MAX_VALUE;
			} else {
				charItem.initDelay = (int) (Math.random() * maxDelayNum);
			}
			charList.add(charItem);
		}
	}

	@Override
	protected void onFinishInflate() {
		new UIThread().start();
	}

	@Override
	protected void onDraw(Canvas canvas) {
		int fromX = getPaddingLeft();
		int fromY = getPaddingTop() + firstLineOffset; // 这个y坐标很恶心，请转到firstLineOffset那里

		// 单行允许装载文字的最大宽度
		int maxLineWidth = getWidth() - getPaddingLeft() - getPaddingRight();

		// 临时的每一行的宽度，动态改变
		int thisLineWidth = 0;
		for (CharHolder itemHolder : charList) {

			// 1. 计算drawText的x/y位置
			String drawChar = itemHolder.charTxt;
			thisLineWidth = thisLineWidth + itemHolder.measureWidth; // 不管三七二十一，先加上这个宽度再说

			if (drawChar.equals("\n")) {
				// 遇到换行
				fromY = fromY + textSize + lineSpaceExtra;
				fromX = getPaddingLeft();
				thisLineWidth = 0;
				continue;
			} else if (thisLineWidth > maxLineWidth) {
				// 这一行满载，装不下了
				fromY = fromY + textSize + lineSpaceExtra;
				fromX = getPaddingLeft();
				thisLineWidth = itemHolder.measureWidth; // 请注意这个临时宽度不是0
			}

			// 2. 计算alpha值
			int alpha = 0;
			int delayInterval = cycleNum - itemHolder.initDelay;
			if (delayInterval > finishCycleNum) {
				alpha = 255;
			} else if (delayInterval > 0) {
				alpha = 255 * (cycleNum - itemHolder.initDelay)
						/ finishCycleNum;
			}

			// 3. 调用drawText输出文字
			if (alpha > 0) {
				// alpha大于零的时候才drawText，节省资源吧
				paint.setAlpha(alpha);
				canvas.drawText(drawChar, fromX, fromY, paint);
			}

			fromX += itemHolder.measureWidth;
		}

		paint.setAlpha(255);

	}

	// 文字装载工具类
	class CharHolder {
		String charTxt; // 单个文字
		int measureWidth = 0; // 文字的宽度
		int initDelay; // 文字delay显示的次数
	}

	class UIThread extends Thread {

		public UIThread() {
			cycleNum = 0;
		}

		@Override
		public void run() {
			int maxCyleNum = finishCycleNum + maxDelayNum;

			try {
				// 睡400毫秒，视觉残留
				sleep(400);

				while (cycleNum < maxCyleNum) {
					sleep(60);

					// handler通知ui更新文字透明度
					Message msg = uiHandler.obtainMessage();
					cycleNum++;
					msg.sendToTarget();
				}
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		};
	};

	private Handler uiHandler = new Handler() {

		@Override
		public void handleMessage(Message msg) {
			// 刷新View
			invalidate();
		};
	};

	@Override
	protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
		super.onMeasure(widthMeasureSpec, heightMeasureSpec);

		// 复写onMeasure方法，没有子View，手动计算高度
		setMeasuredDimension(getMeasuredWidth(),
				getMeasureHeight(heightMeasureSpec));
	}

	/**
	 * 测量该View的高度
	 */
	private int getMeasureHeight(int heightMeasureSpec) {
		int result = 0;
		int size = MeasureSpec.getSize(heightMeasureSpec);
		int mode = MeasureSpec.getMode(heightMeasureSpec);
		if (mode == MeasureSpec.EXACTLY) {
			// 这个是直接写死了，比如xml文件中写成了android:layout_height="200dp"
			result = size;
		} else {
			result = computeViewHeight();
		}
		return result;
	}

	/**
	 * 根据文字和字号测量高度
	 */
	private int computeViewHeight() {
		int widgetHeight = getPaddingTop() + getPaddingBottom() + textSize;
		int maxTextWidth = getMeasuredWidth() - getPaddingLeft()
				- getPaddingRight();
		int thisLineWidth = 0;
		for (CharHolder txtHolder : charList) {
			String charTxt = txtHolder.charTxt;
			if ("\n".equals(charTxt)) {
				// 字符串当中遇到换行
				thisLineWidth = 0;
				widgetHeight = widgetHeight + lineSpaceExtra + textSize;
				continue;
			}

			thisLineWidth += txtHolder.measureWidth;
			if (thisLineWidth > maxTextWidth) {
				// 宽度计算超出边界
				thisLineWidth = txtHolder.measureWidth;
				widgetHeight = widgetHeight + lineSpaceExtra + textSize;
			}
		}
		return widgetHeight;
	}

	public String getShowText() {
		return showText;
	}

	public void setShowText(String showText) {
		this.showText = showText;

		initCharList();
		requestLayout();

		new UIThread().start();
	}
}
