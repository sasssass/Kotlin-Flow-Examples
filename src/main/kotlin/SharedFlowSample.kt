import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*


lateinit var collectingJob : Job

fun main() = runBlocking{

    val sharedFlowHelper = SharedFlowHelper()


    launch {
        sharedFlowHelper.emit()
    }

     collectingJob = launch {
        sharedFlowHelper.sharedFlowData.collect{
            println(it)
            collectingJob.cancel() // if we won't cancel this job the program won't stop
                                   // because of collecting
        }
    }

    launch {
        sharedFlowHelper.emit()
    }

    print("")
}



class SharedFlowHelper{
    private val _sharedFlowData : MutableSharedFlow<String> = MutableSharedFlow()
    val sharedFlowData = _sharedFlowData.asSharedFlow()

    var counter = 0

    suspend fun emit(){
        println("Start to emit")
        _sharedFlowData.emit("Hi I'm Shared Flow number ${++counter}")
    }
}


