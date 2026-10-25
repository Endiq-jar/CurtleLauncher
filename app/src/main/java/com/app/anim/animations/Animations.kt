package com.app.anim.animations

import com.app.anim.animations.bounce.BounceEnlargeAnimator
import com.app.anim.animations.bounce.BounceInDownAnimator
import com.app.anim.animations.bounce.BounceInLeftAnimator
import com.app.anim.animations.bounce.BounceInRightAnimator
import com.app.anim.animations.bounce.BounceInUpAnimator
import com.app.anim.animations.bounce.BounceShrinkAnimator
import com.app.anim.animations.fade.FadeInAnimator
import com.app.anim.animations.fade.FadeInDownAnimator
import com.app.anim.animations.fade.FadeInLeftAnimator
import com.app.anim.animations.fade.FadeInRightAnimator
import com.app.anim.animations.fade.FadeInUpAnimator
import com.app.anim.animations.fade.FadeOutAnimator
import com.app.anim.animations.fade.FadeOutDownAnimator
import com.app.anim.animations.fade.FadeOutLeftAnimator
import com.app.anim.animations.fade.FadeOutRightAnimator
import com.app.anim.animations.fade.FadeOutUpAnimator
import com.app.anim.animations.other.PulseAnimator
import com.app.anim.animations.other.ShakeAnimator
import com.app.anim.animations.other.WobbleAnimator
import com.app.anim.animations.slide.SlideInDownAnimator
import com.app.anim.animations.slide.SlideInLeftAnimator
import com.app.anim.animations.slide.SlideInRightAnimator
import com.app.anim.animations.slide.SlideInUpAnimator
import com.app.anim.animations.slide.SlideOutDownAnimator
import com.app.anim.animations.slide.SlideOutLeftAnimator
import com.app.anim.animations.slide.SlideOutRightAnimator
import com.app.anim.animations.slide.SlideOutUpAnimator
import com.app.anim.animations.zoom.ZoomInAnimator
import com.app.anim.animations.zoom.ZoomInDownAnimator
import com.app.anim.animations.zoom.ZoomInLeftAnimator
import com.app.anim.animations.zoom.ZoomInRightAnimator
import com.app.anim.animations.zoom.ZoomInUpAnimator
import com.app.anim.animations.zoom.ZoomOutAnimator
import com.app.anim.animations.zoom.ZoomOutDownAnimator
import com.app.anim.animations.zoom.ZoomOutLeftAnimator
import com.app.anim.animations.zoom.ZoomOutRightAnimator
import com.app.anim.animations.zoom.ZoomOutUpAnimator
import com.app.anim.animations.sheet.SheetInAnimator
import com.app.anim.animations.sheet.SheetOutAnimator


enum class Animations(val animator: BaseAnimator) {
    //Bounce
    BounceInDown(BounceInDownAnimator()),
    BounceInLeft(BounceInLeftAnimator()),
    BounceInRight(BounceInRightAnimator()),
    BounceInUp(BounceInUpAnimator()),
    BounceEnlarge(BounceEnlargeAnimator()),
    BounceShrink(BounceShrinkAnimator()),

    //Fade in
    FadeIn(FadeInAnimator()),
    FadeInLeft(FadeInLeftAnimator()),
    FadeInRight(FadeInRightAnimator()),
    FadeInUp(FadeInUpAnimator()),
    FadeInDown(FadeInDownAnimator()),

    //Fade out
    FadeOut(FadeOutAnimator()),
    FadeOutLeft(FadeOutLeftAnimator()),
    FadeOutRight(FadeOutRightAnimator()),
    FadeOutUp(FadeOutUpAnimator()),
    FadeOutDown(FadeOutDownAnimator()),

    //Slide in
    SlideInLeft(SlideInLeftAnimator()),
    SlideInRight(SlideInRightAnimator()),
    SlideInUp(SlideInUpAnimator()),
    SlideInDown(SlideInDownAnimator()),

    //Slide out
    SlideOutLeft(SlideOutLeftAnimator()),
    SlideOutRight(SlideOutRightAnimator()),
    SlideOutUp(SlideOutUpAnimator()),
    SlideOutDown(SlideOutDownAnimator()),


    //Zoom - the animation library shipped with no zoom animators at all, so these are new
    // (see ZoomInAnimator et al). Direction variants also drift in from that side.
    ZoomIn(ZoomInAnimator()),
    ZoomInDown(ZoomInDownAnimator()),
    ZoomInLeft(ZoomInLeftAnimator()),
    ZoomInRight(ZoomInRightAnimator()),
    ZoomInUp(ZoomInUpAnimator()),
    ZoomOut(ZoomOutAnimator()),
    ZoomOutDown(ZoomOutDownAnimator()),
    ZoomOutLeft(ZoomOutLeftAnimator()),
    ZoomOutRight(ZoomOutRightAnimator()),
    ZoomOutUp(ZoomOutUpAnimator()),
    SheetIn(SheetInAnimator()),
    SheetOut(SheetOutAnimator()),

    //Other
    Pulse(PulseAnimator()),
    Wobble(WobbleAnimator()),
    Shake(ShakeAnimator())
}