import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*


lateinit var collectingStateFlowJob : Job

fun main() = runBlocking{

    // here you will collect both emission
    // why? because it's Hot-Stream Flow, but it has State too
    // so whenever a collector collects that it will get the latest value as State (which is 1)

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


