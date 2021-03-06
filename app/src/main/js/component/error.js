import React, {Component} from 'react';
import {Image, StyleSheet, Text, TouchableWithoutFeedback, View} from 'react-native';
import TextStyle from '../style/textStyle';
import PropTypes from 'prop-types';

export default class Error extends Component {

    constructor(props) {
        super(props);
    }

    render() {
        return (
            <TouchableWithoutFeedback
                onPress={this.props.onRetry}>
                <View style={styles.root}>
                    <Image
                        resizeMode='contain'
                        source={{uri: 'network_error'}}
                        style={{width: 160, height: 160, marginTop: 32}}/>
                    <Text style={[{marginTop: 24}, TextStyle.body]}>{`Unexpected error`}</Text>
                    <Text style={[{marginTop: 8}, TextStyle.body]}>{`Tap to retry`}</Text>

                </View>
            </TouchableWithoutFeedback>
        );
    }
}

const styles = StyleSheet.create({
    root: {
        alignItems: 'center',
        justifyContent: 'center',
        flex: 1
    },

});

Error.propTypes = {
    onRetry: PropTypes.func,
    errorMessage: PropTypes.string,
    explainMessage: PropTypes.string
};