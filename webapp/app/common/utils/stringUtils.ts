export default {
    replaceTemplate : (str : string, obj: any): string => str.replace(/\${(.*?)}/g, (x,g)=> obj[g])
}