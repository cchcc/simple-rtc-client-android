package cchcc.simplertc

import android.content.res.Resources
import android.support.v7.widget.RecyclerView
import android.view.View
import org.hamcrest.Description
import org.hamcrest.Matcher
import org.hamcrest.TypeSafeMatcher


class RecyclerViewMatcher(val viewId: Int) {

    fun atPosition(position: Int): Matcher<View> {
        return atPositionOnView(position, -1)
    }

    fun atPositionOnView(position: Int, targetViewId: Int): Matcher<View> {

        return object : TypeSafeMatcher<View>() {
            private var resources: Resources? = null
            private var childView: View? = null

            override fun describeTo(description: Description) {
                var idDescription = Integer.toString(viewId)
                if (this.resources != null) {
                    try {
                        idDescription = this.resources!!.getResourceName(viewId)
                    } catch (var4: Resources.NotFoundException) {
                        idDescription = String.format("%s (resource name not found)",
                                *arrayOf<Any>(Integer.valueOf(viewId)))
                    }

                }

                description.appendText("with id: " + idDescription)
            }

            override fun matchesSafely(view: View): Boolean {

                this.resources = view.getResources()

                if (childView == null) {
                    val rv = view.rootView.findViewById(viewId) as? RecyclerView

                    if (rv != null)
                        childView = rv.getChildAt(position)
                    else
                        return false

                }

                if (targetViewId == -1) {
                    return view === childView
                } else {
                    val targetView = childView!!.findViewById(targetViewId)
                    return view === targetView
                }

            }
        }
    }

}