import { NativeModules } from 'react-native';

type MediastoreFileType = {
  isDirectory: boolean,
  id: number,
  name: string,
  duration: number,
  size: number,
  mime: string,
  title: string,
  album: string,
  artist: string,
  genreId: string,
  genreName: string,
  contentUri: string,
  path: string
}

type MediastoreType = {
  readAudioVideoExternalMedias(path: string): Promise<Array<MediastoreFileType>>;
};

const { Mediastore } = NativeModules;

export default Mediastore as MediastoreType;
