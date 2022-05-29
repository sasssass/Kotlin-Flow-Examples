import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*


lateinit var collectingJob : Job

fun main() = runBlocking{


    // here you will only collect the second emission
    // why? because it's Hot-Stream Flow, so the emitter emits and doesn't care about subscriber (collector)
    // the collector won't collect first value because it's attached to emitter after the first emit

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


