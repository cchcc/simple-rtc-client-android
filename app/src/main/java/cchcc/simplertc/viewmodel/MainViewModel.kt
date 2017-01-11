package cchcc.simplertc.viewmodel

import rx.Observable

interface MainViewModel : LifeCycle {
    fun checkServerIsOn(): Observable<Boolean>
}