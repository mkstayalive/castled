export class ObjectUtils {
    static objectEquals(source: any, target: any): boolean {
        if (source == target) {
            return true;
        }
        if (!source || !target) {
            return false;
        }
        if(typeof(source)!== 'object'){
            return false;
        }
    
        if (Object.keys(source) && Object.keys(source).length > 0) {
            return Object.keys(source).every(key => source.hasOwnProperty(key) && this.objectEquals(source[key], target[key]));
        }
        return false;
    }
}