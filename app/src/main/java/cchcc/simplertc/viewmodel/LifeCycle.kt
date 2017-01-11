package cchcc.simplertc.viewmodel

import com.github.salomonbrys.kodein.Kodein

interface LifeCycle {
    fun onCreate(kodein: Kodein) {}
    fun onDestroy() {}
}