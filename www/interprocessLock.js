
/**
 * @constructor
 */
function InterprocessLock() {
    
}

InterprocessLock.prototype.lock = function(lockName, successCallback, errorCallback) {
    exec(successCallback, errorCallback, "InterprocessLock", "lock", [lockName]);
};
InterprocessLock.prototype.release = function(lockName, successCallback, errorCallback) {
    exec(successCallback, errorCallback, "InterprocessLock", "release", [lockName]);
};
module.exports = new InterprocessLock();
