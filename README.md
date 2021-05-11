# react-native-mediastore

React Native Media Store

## Installation

```sh
npm install react-native-mediastore
```

## Usage

```js
import Mediastore from "react-native-mediastore";

// ...

const result = await Mediastore.readAudioVideoExternalMedias();
```

```js
type MediastoreFileType = {
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
  contentUri: string
}

type MediastoreType = {
  readAudioVideoExternalMedias(): Promise<Array<MediastoreFileType>>;
};
```

## Contributing

See the [contributing guide](CONTRIBUTING.md) to learn how to contribute to the repository and the development workflow.

## License

MIT
