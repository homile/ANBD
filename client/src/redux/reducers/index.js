import { combineReducers } from "redux"; // 여러 reducer 들을 하나의 store에 저장 할 수 있게 해주는 함수
import loginReducer from "./loginReducer";
import filtersReducer from "./filtersReducer";

const rootReducer = combineReducers({
  loginReducer,
  filtersReducer,
});
export default rootReducer;
