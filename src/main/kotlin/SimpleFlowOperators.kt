import kotlinx.coroutines.Dispatchers.IO
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.runBlocking

fun main() = runBlocking {
    opAsFlow() // result = 1,2,3,4

    oppFlowOn()

    opTransform() // result = 1.0 10 2.0 20 3.0 30

    opMap() // result = 1.0 2.0 3.0

    opTake() // result = 1

    opReduce() // result = 6

    opFilter() // result = 2
}

//  this operator can cast a list/array/... to a flow
suspend fun opAsFlow(){
    val listOfInt = listOf(1,2,3,4)
    listOfInt.asFlow().collect{
        println(it)
    }
}

// this operator will change dispatcher
// the important thing is the dispatcher is changed for anything
// above flowOn, so it doesn't affect on anything after flowOn
suspend fun oppFlowOn(){
    simpleFlow().flowOn(IO).collect{
        // sth
    }
}

//  by using this operator you can emit a new type instead of old type
suspend fun opTransform(){
    simpleFlow().transform {
        emit(it.toFloat())
        emit(it * 10)
    }.collect{
        println(it)
    }
}


//  by using this operator you can map your old type to new type
//  it's similar to transform but here you won't emit !!
//  for example in transform you can emit 5 times instead of 1 times
//  but here you just cast your properties
suspend fun opMap(){
    println()
    simpleFlow().map {
        it.toFloat()
    }.collect{
        println(it)
    }
}


//  limit how many times the collector can collect
suspend fun opTake(){
    simpleFlow().take(1).collect{
        println(it)
    }
}
//  assume you want a sum of all your flow's values or etc.
//  in these cases you can use reduce, we have accumulator and value
//  it's like a recursion function, the accumulator is old values
//  and value is the current value
//  this code is same as this :
//    val list = listOf(1,2,3,4,5,6)
//    fun sum(list : List<Int>) : Int{
//        return if(list.size == 1) list[0]
//        else list[0] +  sum(list.subList(1,list.size))
//    }
suspend fun opReduce(){
    val sum = simpleFlow().reduce{accumulator, value ->
        accumulator + value
    }
    println(sum)
}


//  this operator will filter your flow, here we just want even numbers
suspend fun opFilter(){
    simpleFlow().filter {
        it % 2 == 0
    }.collect{
        println(it)
    }
}

fun simpleFlow () = flow {
    emit(1)
    emit(2)
    emit(3)
}

