const path = require('path');
const fs = require('fs');
const webpack = require('webpack');

const MAIN_JS_FOLDER = path.resolve(__dirname, 'src/main/webapp/dev/js/main');
const COMMON_JS_FOLDER = path.resolve(__dirname, 'src/main/webapp/dev/js/common');
const OUTPUT_JS_FOLDER = path.resolve(__dirname, 'src/main/webapp/js');

const entry = {};
const SECONDARY_JS_FILES = ['googleAnalytics', 'index', 'statusMessage', 'studentMotd', 'userMap'];

fs.readdirSync(MAIN_JS_FOLDER).forEach((fileName) => {
    if (fileName.endsWith('.es6')) {
        const fileNameWithoutExt = fileName.replace('.es6', '');
        const filesToBundle = [
            `${MAIN_JS_FOLDER}/${fileName}`,
        ];
        if (SECONDARY_JS_FILES.indexOf(fileNameWithoutExt) === -1) {
            filesToBundle.push(`${COMMON_JS_FOLDER}/onStart.es6`);
        }
        entry[fileNameWithoutExt] = filesToBundle;
    }
});

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
    plugins: [
        new webpack.optimize.UglifyJsPlugin(),
    ],
};
