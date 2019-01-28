# android-character-animation
in textview, characters come along one by one with random alpha animation<br/>
### 要实现的App原型
ios里面有一款app叫Mono，用户体验很不错，有兴趣的朋友可以下载下来感受一下。<br>
Mono里面有一种效果是这样的：<br>
列表滑动到某个位置时，图片中间的文字产生随机动画，虽然不是有规律性的错落有致，但还是很有美感。Mono的那种文字动画，不能从传统的动画思路着手，因为传统的动画根本不支持。<br>
### 效果图
这两天刚好项目不大紧张，所以就在空闲时间翻阅了相关资料，对要实现的效果进行了一些思考。<br>
然后，临时起意，写了一些简单的代码，大致实现了如下的效果：<br>
<img src="screen.gif" width="320" height="200" /><br>
看起来还不错吧。（gif有点卡，实际安装不会）<br>

### 下载地址
有兴趣的朋友，可以下载demo试玩，还是比较流畅的：<br>
[Demo下载](TextViewAnimation.apk) (就在github工程之中)

### 使用方法
```xml
<com.stone.textview.animation.AnimTextView
            android:id="@+id/animText"
            android:layout_width="fill_parent"
            android:layout_height="wrap_content"
            android:paddingBottom="5dp"
            android:paddingTop="5dp"
            android:layout_marginRight="70dp"
            textAnim:lineSpaceExtra="15dp"
            textAnim:showText="天下风云出我辈，一入江湖岁月催"
            textAnim:textColor="#eee"
            textAnim:textSize="16sp" />
```
此外，还可以通过java代码手动调用setShowText方法，则会重新计算高度并刷新整个view，触发新一轮的动画。<br><br>
需要注意的是：<br>
AnimTextView请尽量给一个paddingTop和paddingBottom，因为这个View高度的计算不是特别精确，怪就怪在canvas.drawText的的起始坐标无法让人理解。在onMeasure测量高度的时候，使用android:layout_height="wrap_content"在计算高度时，可能会有稍微的一丢丢的偏差(很小).<br>
此外，andorid:layout_with属性可以直接设置一个宽度，或者设置成fill_parent。<br>

如果还有其它问题，请联系邮箱: 120809170@qq.com
