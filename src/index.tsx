import { NativeModules } from 'react-native';

type MediastoreType = {
  multiply(a: number, b: number): Promise<number>;
};

const { Mediastore } = NativeModules;

export default Mediastore as MediastoreType;
