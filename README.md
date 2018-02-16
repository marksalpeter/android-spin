android spin is a port of spin.js for android

# Demo
heres is an example of the kind of spinners you can render with this libary:
![spin](https://user-images.githubusercontent.com/1033500/36289015-7d4e0b2e-1272-11e8-9cbe-e28ffec83ad4.gif)

You can also tweak the parameters to see what the spinner will look like ahead of time in the original [spin.js.org](http://spin.js.org) project to see the different kinds of spinners this library can render

# How to install 
add the followig to gradle to install
```
allprojects{
    repositories {
        jcenter()
        maven { url "https://jitpack.io" }
    }
}
```
```
dependencies {
    ...
    compile 'com.github.marksalpeter:andriod-spin:0.1.3
}
```

# Example
the following `layout.xml` file will render this spinner animation found in the demo section:
```xml
<?xml version="1.0" encoding="utf-8"?>
<RelativeLayout 
	xmlns:android="http://schemas.android.com/apk/res/android"
	xmlns:spin="http://schemas.android.com/apk/res-auto"
	android:layout_width="match_parent"
	android:layout_height="match_parent">

    <com.marksalpeter.spin.SpinView
        android:id="@+id/spinner_view"
        android:layout_width="wrap_content"
        android:layout_height="wrap_content"
        android:layout_centerInParent="true"
        spin:radius="30dp"
        spin:length="35dp"
        spin:speed="1"
        spin:corners="0.3"
        spin:lines="12"
        spin:width="8dp"/>

</RelativeLayout>
```