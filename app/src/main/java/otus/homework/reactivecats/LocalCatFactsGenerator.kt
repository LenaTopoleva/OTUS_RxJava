package otus.homework.reactivecats

import android.content.Context
import io.reactivex.Flowable
import io.reactivex.BackpressureStrategy
import io.reactivex.Observable
import java.util.concurrent.TimeUnit

class LocalCatFactsGenerator(
    private val context: Context
) {

    /**
     * Реализуйте функцию otus.homework.reactivecats.LocalCatFactsGenerator#generateCatFact так,
     * чтобы она возвращала Fact со случайной строкой  из массива строк R.array.local_cat_facts
     * обернутую в подходящий стрим(Flowable/Single/Observable и т.п)
     */
    fun generateCatFact(): Observable<Fact> {
        return Observable.just(Fact(getRandomFact()))
    }

    /**
     * Реализуйте функцию otus.homework.reactivecats.LocalCatFactsGenerator#generateCatFactPeriodically так,
     * чтобы она эмитила Fact со случайной строкой из массива строк R.array.local_cat_facts каждые 2000 миллисекунд.
     * Если вновь заэмиченный Fact совпадает с предыдущим - пропускаем элемент.
     */
    fun generateCatFactPeriodically(): Flowable<Fact> =
        Observable.interval(0, 2000, TimeUnit.MILLISECONDS)
            .flatMap { Observable.just(Fact(getRandomFact())) }
            .toFlowable(BackpressureStrategy.DROP).distinctUntilChanged()

    private fun getRandomFact() = context.resources.getStringArray(R.array.local_cat_facts).random()
}