import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*


lateinit var collectingStateFlowJob : Job

fun main() = runBlocking{

    val stateFlowHelper = StateFlowHelper()


    launch {
        stateFlowHelper.emit()
    }

    var collectingCounter = 0

    collectingStateFlowJob = launch {
        stateFlowHelper.stateFlowData.collect{ collected ->
            collected?.let {
                println(it)
                if(++collectingCounter == 2)
                    collectingStateFlowJob.cancel() // if we won't cancel this job the program won't stop
                                                    // because of collecting
            }
        }
    }

    launch {
        stateFlowHelper.emit()
    }

    print("")
}



class StateFlowHelper{
    private val _stateFlowData : MutableStateFlow<String?> = MutableStateFlow(null)
    val stateFlowData = _stateFlowData.asStateFlow()

    var counter = 0

    suspend fun emit(){
        println("Start to emit")
        _stateFlowData.emit("Hi I'm State Flow number ${++counter}")
    }
}


