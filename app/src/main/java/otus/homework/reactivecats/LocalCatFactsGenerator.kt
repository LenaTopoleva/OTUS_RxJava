package otus.homework.reactivecats

import android.content.Context
import io.reactivex.Single
import io.reactivex.Flowable
import io.reactivex.FlowableOnSubscribe
import io.reactivex.FlowableEmitter
import io.reactivex.BackpressureStrategy

class LocalCatFactsGenerator(
    private val context: Context
) {

    /**
     * Реализуйте функцию otus.homework.reactivecats.LocalCatFactsGenerator#generateCatFact так,
     * чтобы она возвращала Fact со случайной строкой  из массива строк R.array.local_cat_facts
     * обернутую в подходящий стрим(Flowable/Single/Observable и т.п)
     */
    fun generateCatFact(): Single<Fact> {
        return Single.just(Fact(getRandomFact()))
    }

    /**
     * Реализуйте функцию otus.homework.reactivecats.LocalCatFactsGenerator#generateCatFactPeriodically так,
     * чтобы она эмитила Fact со случайной строкой из массива строк R.array.local_cat_facts каждые 2000 миллисекунд.
     * Если вновь заэмиченный Fact совпадает с предыдущим - пропускаем элемент.
     */
    fun generateCatFactPeriodically(): Flowable<Fact> =
        Flowable.create(object : FlowableOnSubscribe<Fact> {
            override fun subscribe(emitter: FlowableEmitter<Fact>) {
                while (true) {
                    emitter.onNext(Fact(getRandomFact()))
                    Thread.sleep(2000)
                    if (emitter.isCancelled) {
                        return
                    }
                }
            }
        }, BackpressureStrategy.DROP).distinctUntilChanged()

    private fun getRandomFact() = context.resources.getStringArray(R.array.local_cat_facts).random()
}