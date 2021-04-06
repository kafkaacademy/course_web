
import * as actions from './actionTypes'
let lastIndex = 0;

export function reducer(state=[], action) {
    switch (action.type) {

        case actions.BUG_ADDED: {
            return [...state, {
                id: ++lastIndex,
                description: action.payload.description,
                resolved: false
            }]
        }
        case actions.BUG_RESOLVED: {
            return state.map( bug=> bug.id === action.payload.id? {...bug, resolved: true}: bug)
                   
        }
        case actions.BUG_REMOVED: {
            return state.filter(bug => bug.id!== action.payload.id)
            
        }
        default:
            return state;
    }

}