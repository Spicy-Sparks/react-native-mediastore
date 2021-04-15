import { NativeModules } from 'react-native';

type MediastoreFileType = {
  id: number,
  name: string,
  duration: number,
  size: number,
  contentUri: string
}

type MediastoreType = {
  readAudioVideoExternalMedias(): Promise<Array<MediastoreFileType>>;
};

const { Mediastore } = NativeModules;

export default Mediastore as MediastoreType;
