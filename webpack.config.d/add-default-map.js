const path = require('path');

config.resolve.modules.push(path.resolve("../../../../src/commonMain/resources"))

config.module.rules.push({
	test: /\.map$/i,
	use: [ 'raw-loader' ]
})