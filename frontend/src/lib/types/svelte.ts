export type NativeEvent<Event, Target> = Event & { currentTarget: EventTarget & Target };
