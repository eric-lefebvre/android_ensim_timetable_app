package com.example.ensim_app.ui.state

import android.util.Log
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import com.example.ensim_app.domain.Resource
import com.example.ensim_app.service.IcsParser
import com.example.ensim_app.service.IcsService
import com.example.ensim_app.service.PlanningSource
import com.example.ensim_app.service.model.Cours

@Composable
fun rememberCoursForEdt(edtId: Int?): Resource<List<Cours>>? {
    var resource by remember { mutableStateOf<Resource<List<Cours>>?>(null) }

    LaunchedEffect(edtId) {
        if (edtId == null) {
            resource = null
            return@LaunchedEffect
        }
        resource = Resource.Loading
        resource = try {
            val ics = IcsService.fetchIcs(PlanningSource.icsUrl(edtId))
            val cours = IcsParser.parse(ics)
            Log.d("ENSIM", "${cours.size} cours parsés pour #$edtId")
            Resource.Success(cours)
        } catch (e: Exception) {
            Log.e("ENSIM", "Erreur fetch/parse", e)
            Resource.Error
        }
    }

    return resource
}
