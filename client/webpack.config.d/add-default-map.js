const path = require('path');

config.resolve.modules.push(path.resolve("../../../../common/src/commonMain/resources"))

config.module.rules.push({
	test: /\.map$/i,
	use: [ 'raw-loader' ]
})