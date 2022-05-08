import * as React from 'react';
import Permissions from 'react-native-permissions';
import { StyleSheet, View, Text } from 'react-native';
import Mediastore from 'react-native-mediastore';
import Video from 'react-native-video';

export default function App() {

  const [ source, setSource ] = React.useState<any>(null)

  React.useEffect(() => {

    (async () => {
      try {
        const status = await Permissions.request(Permissions.PERMISSIONS.ANDROID.READ_EXTERNAL_STORAGE)
        if(status !== Permissions.RESULTS.GRANTED) {
          console.log("Permission fail", status)
          return
        }

        const files = await Mediastore.readAudioVideoExternalMedias("")
        console.log(files)

        if(files.length > 0) {
          setSource({
            uri: files[0].contentUri
          })
        }
      }
      catch (err) {
        console.log(err)
      }
    })()
  }, []);

  console.log(source)

  return (
    <View style={styles.container}>
      <Text>Hello, MediaStore!</Text>
      { (source !== null) && <Video
        source={source}
        style={{
          width: 400,
          height: 200,
          backgroundColor: "red"
        }}
        onLoad={(data: any) => console.log("onLoad", data)}
        onError={(error: any) => console.log("onError", error)}
      /> }
    </View>
  );
}

const styles = StyleSheet.create({
  container: {
    flex: 1,
    alignItems: 'center',
    justifyContent: 'center',
  },
  box: {
    width: 60,
    height: 60,
    marginVertical: 20,
  },
});
