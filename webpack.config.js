const path = require('path');

module.exports = {
    mode: "development",
    entry: "./stimulus/index.js",
    output: {
        path: path.resolve(__dirname, 'resources/public/js'),
        filename: "gomosdg.hotwire.bundle.js"
    },
    module: {
        rules: [{
            use: {
                loader: "babel-loader",
                options: {
                    presets: ["@babel/preset-env"],
                    plugins: ["@babel/plugin-proposal-class-properties"]
                }
            }
        }]
    }
}
