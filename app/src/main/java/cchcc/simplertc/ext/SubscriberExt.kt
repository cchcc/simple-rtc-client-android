package cchcc.simplertc.ext

import rx.Subscriber

fun <T> Subscriber<T>.onNextAndCompleted(t: T) {
    onNext(t)
    onCompleted()
}