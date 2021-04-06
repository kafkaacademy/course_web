import { store} from './store'
import { bugAdded, bugResolved,bugRemoved} from './actions'

const unsubscribe= store.subscribe(()=> console.log("store changed", store.getState()))
store.dispatch(bugAdded( "my first bug"))
store.dispatch(bugAdded( "my bug 2"))
store.dispatch(bugRemoved( 1))
store.dispatch(bugResolved( 2))


console.log(store.getState())