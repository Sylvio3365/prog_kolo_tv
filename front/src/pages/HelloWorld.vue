<template>
    <div class="hello-world">
        <h1>Hello World from Vue.js</h1>
        <div v-if="loading">Chargement...</div>
        <div v-else-if="error" class="error">{{ error }}</div>
        <div v-else class="message">{{ message }}</div>
    </div>
</template>

<script>
import axios from 'axios'

export default {
    name: 'HelloWorld',
    data() {
        return {
            message: '',
            loading: true,
            error: null
        }
    },
    async mounted() {
        try {
            const response = await axios.get('http://localhost:6060/kolotv/api/hello')
            const data = response.data
            this.message = data.message || 'Message reçu depuis l\'API'
        } catch (err) {
            this.error = 'Erreur lors de l\'appel à l\'API: ' + err.message
        } finally {
            this.loading = false
        }
    }
}
</script>

<style scoped>
.hello-world {
    padding: 20px;
    text-align: center;
}

.message {
    color: green;
    font-size: 18px;
    margin: 20px 0;
}

.error {
    color: red;
    font-size: 16px;
    margin: 20px 0;
}
</style>