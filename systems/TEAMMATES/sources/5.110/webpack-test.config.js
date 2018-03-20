const path = require('path');
const fs = require('fs');

const TEST_JS_FOLDER = path.resolve(__dirname, 'src/main/webapp/dev/js/test');
const COMMON_JS_FOLDER = path.resolve(__dirname, 'src/main/webapp/dev/js/common');
const OUTPUT_JS_FOLDER = path.resolve(__dirname, 'src/main/webapp/test');

const entry = {};
entry.tableSort = [`${COMMON_JS_FOLDER}/onStart.es6`];
entry.jsUnitTests = fs.readdirSync(TEST_JS_FOLDER)
        .filter(fileName => fileName.endsWith('.es6'))
        .map(fileName => `${TEST_JS_FOLDER}/${fileName}`);

const babel = {
    test: /\.(?:js|es6)$/,
    exclude: /(node_modules|bower_components)/,
    use: {
        loader: 'babel-loader',
        options: {
            presets: ['env'],
            cacheDirectory: true,
        },
    },
};

module.exports = {
    entry,
    output: {
        filename: '[name].js',
        path: OUTPUT_JS_FOLDER,
    },
    module: {
        rules: [
            babel,
        ],
    },
    stats: 'errors-only',
};
