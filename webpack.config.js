const path = require('path');
const HtmlWebpackPlugin = require('html-webpack-plugin');
const { CleanWebpackPlugin } = require('clean-webpack-plugin');
const CopyPlugin = require('copy-webpack-plugin');


module.exports = {
    mode: "production",
    entry: {
        app: './src/index.js'
    },
    devtool: 'inline-source-map',
    performance: {
        maxAssetSize: 1500000,
        assetFilter: (asset) => {
          return asset.match('entrypoint1.js');
        }
      },
    plugins: [
        new CleanWebpackPlugin({ cleanStaleWebpackAssets: false }),
        new HtmlWebpackPlugin({title : 'Apache Kafka Course'}),
        new CopyPlugin({
            patterns: [
                { from: './src/data', to: './data' }
            ],
          }),
    ],
    output: {
        filename: '[name].bundle.js',
        path: path.resolve(__dirname, 'dist'),
    },
    module: {
        rules: [
            {
                test: /\.css$/,
                use: [
                    'style-loader',
                    'css-loader',
                ],
            },
            {
                test: /\.(png|svg|jpg|gif)$/,
                use: [
                    'file-loader',
                ],
            },
        ],
    },
};