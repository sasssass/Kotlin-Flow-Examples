import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.runBlocking

fun main() = runBlocking {
    simple().collect { // Collector
        println("Number $it")
    }
    print("")
}

fun simple(): Flow<Int> = flow { // Emitter
    for (i in 1..3) {
        emit(i)
    }
}