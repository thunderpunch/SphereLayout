# SphereLayout

a layout which supports 3d rotate and enable its childview has z-depth for android

the effect is shown as below

|I|II|
|:---:|:---:|
|![](/gif/i.gif)|![](/gif/ii.gif)|

## Usage

**XML**

1. 在布局中配置SphereLayout, 设置半径"radius"和对齐方向"snapOrientation" (属性说明见XML attributes);

2. 添加childview, SphereLayout可以包含任意个childview ,每个childview都需要设置"***layout_depth***"属性,取值范围在 SphereLayout的"radius"正负范围内[ -radius , radius ] , 布局未旋转时,depth越大的childview视觉上越接近用户, depth 为负的childview一开始处于背面.

3. childview默认位于SphereLayout在x,y轴构建的平面上的中间位置，可添加margin使它相对中间位置进行偏移.

```xml
    <!-- sample -->
<com.thunderpunch.spherelayoutlib.layout.SphereLayout
    android:id="@+id/sl"
    android:layout_width="match_parent"
    android:layout_height="400dp"
    app:radius="90dp"
    app:snapOrientation="horizontal">

    <View
        android:id="@+id/v0"
        android:layout_width="match_parent"
        android:layout_height="match_parent"
        android:background="@drawable/shape_oval_grey"
        app:layout_depth="30dp"
        app:layout_fitBounds="true" />

    <View
        android:id="@+id/v1"
        android:layout_width="50dp"
        android:layout_height="50dp"
        android:background="@drawable/shape_oval_black"
        app:layout_depth="-70dp" />

    <LinearLayout
        android:layout_width="90dp"
        android:layout_height="wrap_content"
        android:layout_marginRight="55dp"
        android:layout_marginTop="40dp"
        android:orientation="vertical"
        android:paddingBottom="5dp"
        android:paddingLeft="8dp"
        android:paddingRight="8dp"
        android:paddingTop="5dp"
        app:layout_depth="40dp">

    </LinearLayout>
</com.thunderpunch.spherelayoutlib.layout.SphereLayout>
```

**Code**

4. 使SphereLayout朝某个方向旋转一定的角度

```java
SphereLayout sl = (SphereLayout) findViewById(R.id.sl);
sl.rotate(-40, 30);//朝x轴正方向逆时针旋转40度的方向，向内翻转30度
```

5. 反转 SphereLayout

```java
sl.reverse(true);
```



## XML attributes

- SphereLayout自身属性

```xml
<declare-styleable name="SphereLayout">
    <!-- 标记位于背面的视图是以Y轴水平翻转可到达正面，或是以X轴竖直翻转可到达正面 -->
    <attr name="snapOrientation">
        <enum name="horizontal" value="0" />
        <enum name="vertical" value="1" />
        <enum name="none" value="9" />
    </attr>
    <!-- 球体半径-->
    <attr name="radius" format="dimension|reference" />
    <!--是否隐藏位于背面的视图 -->
    <attr name="hideBack" format="boolean" />
</declare-styleable>    
```

- SphereLayout 直系子视图的布局参数			

```xml
<declare-styleable name="SphereLayout_Layout">
   	<!--Z轴位置，必需设置-->
    <attr name="layout_depth" format="dimension|reference" /> 
  
  	<!--为true时，子视图会宽高适配当前所处depth的圆直径且无视自身margin属性-->
    <attr name="layout_fitBounds" format="boolean" /> 
</declare-styleable>
```



## Extras

- 使SphereLayout和用户进行手势交互

```java
SphereLayout sl = (SphereLayout) findViewById(R.id.sl);
sl.setOnTouchListener(new SLTouchListener(sl));
```

- 使SphereLayout依附于另一个SphereLayout的中心进行翻转 (待完善)

```java
/**
 * @param sphereLayout 翻转依赖
 */
public void rotateDependsOn(SphereLayout sphereLayout) {
    mDependency = sphereLayout;
    mCheckedDependencyOffset = false;
    mDependency.mRotateListener = new OnRotateListener() {
        @Override
        public void onRotate(int direction, int degree) {
            rotate(direction, degree);
        }
    };
}
```



## License

```
MIT License

Copyright (c) 2017 thunderpunch

Permission is hereby granted, free of charge, to any person obtaining a copy
of this software and associated documentation files (the "Software"), to deal
in the Software without restriction, including without limitation the rights
to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
copies of the Software, and to permit persons to whom the Software is
furnished to do so, subject to the following conditions:

The above copyright notice and this permission notice shall be included in all
copies or substantial portions of the Software.

THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
SOFTWARE.
```
