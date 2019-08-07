var path = require('path');
var webpack = require('webpack');
var proxy = require('http-proxy-middleware');

const config = {
    entry: './main.js',
    output: {
        path: __dirname + '/build',
        filename: 'bundle.js',

        //to run with webpack-dev-server
        //output will be http:\\localhost:PORT/build/bundle.js (which is the same as the file disk: /build/bundle.js)
        //ref: https://github.com/webpack/webpack-dev-server/issues/24
        publicPath: '/build'
    },
    module: {
        loaders: [
            {
                // Only run `.js` and `.jsx` files through Babel
                test: /\.js$/,
                loader: 'babel-loader',
                exclude: /(node_modules|bower_components|javascripts)/,
                query: {
                    // presets: ['es2015', 'react'],
                    plugins: ['react-html-attrs']
                }
            }
        ]

    },
    resolve: {
        extensions: ['', '.js', '.jsx']
    },
    devServer: {
        port: 8000,
        historyApiFallback: true
        //the proxy config is to proxy api requests to backend server, instead using the webpack dev server
        // proxy: {
        //     '/api': {
        //         target: 'http://localhost:3000',
        //         secure: false
        //     },
        //     '/upload': {
        //         target: 'http://localhost:3000',
        //         secure: false
        //     }
        // }
    },
    plugins: [
        new webpack.DefinePlugin({
            'process.env.NODE_ENV': JSON.stringify(process.env.NODE_ENV || 'development')
        })
    ]

};

if (process.env.NODE_ENV === 'production') {
    console.log("--- PRODUCTION ---");
    config.plugins.push(
        new webpack.optimize.UglifyJsPlugin({
            compressor: {
                warnings: false
            }
        })
    )
}

module.exports = config;