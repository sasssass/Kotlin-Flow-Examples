import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking

fun main() = runBlocking {

    opBuffer() // result = 1,2,3,4

    opConflate() // result = 1,5

    opCollectLatest() // result = 5

    opZip() // result = 1 1.0 , 2 2.0 , 3 3.0

    opCombine() // result = 1 4.0 , 2 4.0 , 2 5.0 , 3 5.0 , 3 6.0

    opFlatMapConcat() // result = Number 1 is 6.0 , Number 1 is 7.0 , Number 2 is 6.0 , Number 2 is 7.0
                      //          Number 3 is 6.0 , Number 3 is 7.0

    opFlatMapMerge() // result = Number 1 is 6.0 , Number 2 is 6.0 , Number 1 is 7.0 , Number 3 is 6.0
                     //          Number 2 is 7.0 , Number 3 is 7.0

    opFlatMapLatest() // result = Number 3 is 6.0 , Number 3 is 7.0

}

// this is a cold-stream flow
// in cold-stream flow you can't emit unless there was a collector
// in this case collector is taking 2000 ms so the emission has to
// wait 2000 ms too
// if we use buffer then the emitter won't wait 2000 because it will
// buffer data and pass it to the collector at the proper time
suspend fun opBuffer(){
    val bufferedFlow = flow {
        for (i in 1..5){
            println("Emit")
            emit(i)
        }
    }.buffer()

    bufferedFlow.collect{
        delay(2000)
        println(it)
    }
}

// in the previous case assume you don't want to buffer
// instead you want to skip the data if collecting is too slow
// in this case conflate make collector to just collect some of our values (first and last)
// right now it only
suspend fun opConflate(){
    val conflationFlow = flow {
        for (i in 1..5){
            println("Emit")
            emit(i)
        }
    }.conflate()

    conflationFlow.collect{
        delay(2000)
        println(it)
    }
}



// sometimes for some reason (for example when you use db (Room or ...))
// you just want to collect the latest value, you can od it by using collectLatest
// be aware if the collector is running so fast then it will collect all values
// so that's why we still need delay
suspend fun opCollectLatest(){
    val latestFlow = flow {
        for (i in 1..5){
            println("Emit")
            emit(i)
        }
    }

    latestFlow.collectLatest{
        delay(100)
        println(it)
    }
}


// if you want to concat two flows with each other you can use zip
// here one of then has 2 more values and if you run this you can see
// the last 2 Int numbers will be ignored
// if we have 1 , 2 , 3 and another flow with 6 , 7 , 8
// the result would be : 1+6,2+7,3+8
suspend fun opZip(){
    val zipFlowInt = flow {
        for (i in 1..5)
            emit(i)
    }
    val zipFlowDouble = flow {
        for (i in 1..3){
            emit(i.toDouble())
            delay(200)
        }
    }

    zipFlowInt.zip(zipFlowDouble){ int , double ->
        "$int $double"
    }.collect{
        println(it)
    }
}


// in this case if you don't have delay we'll get the same result as zip
// if there is some delay somewhere then we'll get different result
// the timeline is like this :
// combineFlowInt combine with combineFlowDouble TimeLine
// ----100 ms Emit (1) ----150 ms Emit (4.0) --------200 ms Emit (2)----------300 ms Emit(5.0)---------
// ------------------------150 ms combine (1+4.0) ---200 ms combine (2+4.0)---300 ms combine (2+5.0)---
// Timeline Page 2 :
// ----400 ms Emit (3) ---------450 ms Emit (5.0) ------500 ms Emit (3)---------600 ms Emit(6.0)-------
// ----400 ms combine (3+5.0)---450 ms combine (3+5.0)--500 ms combine (3+5.0)--600 ms Emit(3+6.0)-----
//
//
// you can see a new combination will be combined whenever one of the emitters emits a new value
suspend fun opCombine(){
    val combineFlowInt = flow {
        for (i in 1..3){
            delay(100)
            emit(i)
        }
    }
    val combineFlowDouble = flow {
        for (i in 4..6){
            delay(150)
            emit(i.toDouble())
        }
    }

    combineFlowInt.combine(combineFlowDouble){ int , double ->
        "$int $double"
    }.collect{
        println(it)
    }
}


// another operator for combination
// but in this one if we have 1,2 and "a","b","c" then we'll get
// 1->a , 1->b , 1->c , 2->a , 2->b , 2->c
// you can see Zip and Combine won't multiple all rows to all columns but here it will
// flatMap is deprecated, so we should use flatMapConcat
// also here I used a helper map operator too
suspend fun opFlatMapConcat(){
    val flatMapConcatFlowInt = flow {
        for (i in 1..3){
            delay(50)
            emit(i)
        }
    }
    val flatMapConcatFlowDouble = flow{
        for (i in 6..7){
            delay(100)
            emit(i.toDouble())
        }
    }

    flatMapConcatFlowInt.flatMapConcat {int->
        flatMapConcatFlowDouble.map {double->
            "Number $int is $double"
        }
    }.collect{
        println(it)
    }
}


// another form of flatMap but here it won't wait for delay, it will merge as soon as
// one the emitter emits some value so the result won't be in order
suspend fun opFlatMapMerge(){
    val flatMapMergeFlowInt = flow {
        for (i in 1..3){
            delay(50)
            emit(i)
        }
    }
    val flatMapMergeFlowDouble = flow{
        for (i in 6..7){
            delay(100)
            emit(i.toDouble())
        }
    }

    flatMapMergeFlowInt.flatMapMerge {int->
        flatMapMergeFlowDouble.map {double->
            "Number $int is $double"
        }
    }.collect{
        println(it)
    }
}


// another form of flatMap but here it only processes the latest values
// so some combination will be ignored
// one the emitter emits some value so the result won't be in order
suspend fun opFlatMapLatest(){
    val flatMapLatestFlowInt = flow {
        for (i in 1..3){
            delay(50)
            emit(i)
        }
    }
    val flatMapLatestFlowDouble = flow{
        for (i in 6..7){
            delay(100)
            emit(i.toDouble())
        }
    }

    flatMapLatestFlowInt.flatMapLatest {int->
        flatMapLatestFlowDouble.map {double->
            "Number $int is $double"
        }
    }.collect{
        println(it)
    }
}




